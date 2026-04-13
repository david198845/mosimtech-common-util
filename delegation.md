# Architektur-Spezifikation: Hybrides Delegations-System (UMA 2.0)

**Projekt:** Momasoft Microservices  
**Technologie-Stack:** Spring Boot 3, Angular, Keycloak 26 (UMA 2.0), RabbitMQ  
**Zweck:** Feingranulare, sichere Freigabe von Modul-Ressourcen (Schichten, Finanzen) zwischen System-Benutzern.

---

## 1. Fachliche Spezifikation & Lifecycle

Der Lebenszyklus einer Delegation besteht aus vier Phasen:

1. **Initiierung (User Service):** User A (Grantor) erstellt eine Freigabe für User B (Grantee) mit spezifischen Rechten (z. B. `shift:view`).
2. **Provisionierung (Async):** Der User Service informiert das jeweilige Fach-Modul (z. B. Shift-Service). Das Fach-Modul kommuniziert mit Keycloak, um die UMA-Ressource und Policy physisch anzulegen.
3. **Nutzung (Frontend & Backend):** User B fragt beim Backend explizit die Daten von User A an (Context-Switching).
4. **Widerruf (Revocation):** User A entzieht die Rechte. Die UMA-Policy in Keycloak wird gelöscht, sofortige Zugriffsverweigerung greift.

---

## 2. Keycloak UMA 2.0 Datenmodell

Jedes Fach-Modul agiert in Keycloak als **Resource Server**.

### 2.1 Ressourcen & Scopes
Damit Keycloak Rechte verwalten kann, muss das Fach-Modul die Ressourcen anmelden:
* **Resource Name:** `res_shift_user_<Grantor_UUID>` (Repräsentiert alle Schichten von User A).
* **Type:** `urn:momasoft:shift`
* **Scopes:** `shift:view`, `shift:edit`

### 2.2 UMA Policies
Wenn eine Freigabe erteilt wird, generiert das Backend via Protection API eine Policy:
* **Policy Name:** `pol_grant_<Grantor_UUID>_to_<Grantee_UUID>_shift_view`
* **Logik:** User-Policy -> Evaluiert auf die Keycloak-ID von User B.
* **Permission Ticket:** Verknüpft die Resource `res_shift_user_<A>` mit dem Scope `shift:view` und der Policy für User B.

---

## 3. Backend-Architektur (Spring Boot)

Das Herzstück der Architektur ist die `momasoft-common-security` Library, die in alle Module eingebunden wird.

### 3.1 Der Security Context (ThreadLocal)
Um Controllers und Services nicht mit fremden IDs zu "verschmutzen", nutzen wir einen Thread-gebundenen Kontext.

```java
public class DelegationContextHolder {
    private static final ThreadLocal<String> TARGET_USER_ID = new ThreadLocal<>();

    public static void setTargetUserId(String userId) { TARGET_USER_ID.set(userId); }
    public static String getTargetUserId() { return TARGET_USER_ID.get(); }
    public static void clear() { TARGET_USER_ID.remove(); }
}
```

### 3.2 Der Header-Filter (`OncePerRequestFilter`)
Dieser Filter fängt Requests ab, die delegierte Daten anfordern.

```java
@Component
public class DelegationHeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String targetUserId = request.getHeader("X-Target-User-Id");
        
        if (targetUserId != null && !targetUserId.isBlank()) {
            DelegationContextHolder.setTargetUserId(targetUserId);
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Wichtig: Memory Leaks und Context-Bleeding verhindern!
            DelegationContextHolder.clear(); 
        }
    }
}
```

### 3.3 Der "Effective User" Provider
Services nutzen diesen Provider, um die Datenbank-Queries zu filtern. Er entscheidet automatisch, ob die Daten des eingeloggten Users oder die delegierten Daten geladen werden sollen.

```java
@Component
public class CurrentUserProvider {
    public String getEffectiveUserId() {
        // 1. Prüfe ob wir im Namen eines anderen agieren
        String delegatedId = DelegationContextHolder.getTargetUserId();
        if (delegatedId != null) {
            return delegatedId;
        }
        // 2. Fallback: Eigene ID aus dem JWT (SecurityContextHolder)
        return extractUserIdFromJwt();
    }
}
```

### 3.4 Spring Security Konfiguration (`@PreAuthorize`)
Im Controller muss nun zwingend geprüft werden, ob das übermittelte RPT-Token (Requesting Party Token) den Zugriff auf die Fremddaten erlaubt.

```java
@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    // Prüft, ob der User das UMA-Recht für die übermittelte X-Target-User-Id hat
    @PreAuthorize("hasPermission(#targetUserId, 'urn:momasoft:shift', 'shift:view')")
    @GetMapping
    public List<ShiftDto> getShifts(@RequestHeader(value = "X-Target-User-Id", required = false) String targetUserId) {
        
        // currentUserProvider liefert hier automatisch den targetUserId
        String effectiveId = currentUserProvider.getEffectiveUserId(); 
        return shiftRepository.findByUserId(effectiveId);
    }
}
```

---

## 4. RabbitMQ Event-Spezifikation

Da der *User Service* die Einladungen orchestriert, aber das *Shift-Modul* die UMA-Ressource in Keycloak anlegen muss, kommunizieren sie über Events.

**Exchange:** `iam.events.topic`  
**Routing Key:** `delegation.status.changed`

**Payload:**
```json
{
  "eventId": "a1b2c3d4-...",
  "timestamp": "2026-04-13T10:00:00Z",
  "eventType": "DELEGATION_ACCEPTED",
  "grantor": {
    "userId": "uuid-user-a",
    "name": "Max Mustermann"
  },
  "grantee": {
    "userId": "uuid-user-b",
    "email": "anna@example.com"
  },
  "module": "SHIFT_CALENDAR",
  "scopes": ["shift:view"]
}
```
*Das Zielmodul (hier SHIFT_CALENDAR) konsumiert dieses Event und ruft synchron über einen Service-Account die Keycloak Protection API auf, um die Policy `pol_grant_<A>_to_<B>` anzulegen.*

---

## 5. Frontend-Architektur (Angular)

### 5.1 Token Exchange (UMA)
Wenn das Frontend auf fremde Daten zugreifen will, reicht das normale Access Token nicht aus. Es muss beim Keycloak Token-Endpoint gegen ein **RPT (Requesting Party Token)** eingetauscht werden.

**Request an Keycloak Token Endpoint:**
```http
POST /realms/momasoft/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:uma-ticket
&audience=momasoft-shift-service
```
*(Keycloak gibt ein RPT-JWT zurück, das im Claim `authorization` die delegierten Freigaben enthält).*

### 5.2 Daten-Aggregation (`forkJoin`)
Die Angular-Komponente lädt die eigene Daten (mit normalem Token) und fremde Daten (mit RPT-Token + Header) parallel.

```typescript
loadCalendarData() {
  // 1. Eigene Schichten (Nutzt den Standard-JWT-Interceptor)
  const myShifts$ = this.http.get('/api/v1/shifts').pipe(
    map(data => this.formatEvent(data, '#3788d8', 'Ich')),
    catchError(() => of([]))
  );

  // 2. Fremde Schichten (Nutzt speziellen Interceptor, der das RPT-Token anhängt)
  const foreignShifts$ = this.activeDelegations.map(del => {
    const headers = new HttpHeaders().set('X-Target-User-Id', del.grantorId);
    
    return this.http.get('/api/v1/shifts', { headers }).pipe(
      map(data => this.formatEvent(data, del.color, del.grantorName)),
      catchError(() => of([])) // Verhindert White-Screen bei entzogenen Rechten
    );
  });

  // 3. Warten bis alle Daten da sind und flach zusammenführen
  forkJoin([myShifts$, ...foreignShifts$]).subscribe(results => {
    this.calendarEvents = results.flat();
  });
}
```

---

## 6. Sicherheitsbetrachtungen (Security Considerations)

1. **Spoofing der X-Target-User-Id:** Das Setzen des Headers durch das Frontend ist per se unsicher. Die absolute Sicherheit wird **ausschließlich** durch die `@PreAuthorize`-Annotation und die Validierung des UMA-RPT-Tokens im Backend gewährleistet. Wenn ein User eine fremde ID sendet, für die Keycloak kein RPT-Ticket ausstellt, lehnt das Backend den Request mit HTTP 403 Forbidden ab.
2. **Keycloak Application Initiated Actions (AIA):** Die 2FA-Einrichtung und Passwortänderungen erfolgen über den Login-Flow (`action: 'CONFIGURE_TOTP'`). Dadurch entfällt ein teurer Custom-Build der Keycloak Account Console und eine Step-Up Authentication (erneute Passwort-Eingabe) wird für höchste Sicherheit erzwungen.