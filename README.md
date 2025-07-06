# 🚀 ColdMailService

Automated bulk cold-emailing via Gmail & Google Sheets, built with Spring Boot.

---

## 💻 Features

- OAuth2 Google authorization (per Gmail sender)
- Parse and map Google Sheet rows to `EmailDTO`
- Render personalized templates using row data
- Send bulk emails and track **SENT** status
- Optional ✅ email open tracking via image pixel
- Update Sheet cell values (`STATUS`, `SENT_DATE`, `TRACKING_ID`) from Java

---

## 🔧 Setup Instructions

### 1. Clone Repo

```bash
git clone https://github.com/Pushpajit/coldmailservice.git
cd coldmailservice
```

### 2. Set Up Google APIs

- Enable **Gmail API** & **Sheets API** in Google Cloud Console
- Create OAuth 2.0 Client (Desktop/Web), set redirect URI to:

  ```
  http://localhost:8080/Callback
  ```

- Download `credentials.json` → place in:

  ```
  src/main/resources/credentials/credentials.json
  ```

### 3. Add API Scopes

Ensure `GmailConfigService` includes:
```java
List<String> SCOPES = List.of(
  GmailScopes.GMAIL_READONLY,
  GmailScopes.GMAIL_SEND,
  GmailScopes.GMAIL_LABELS,
  SheetsScopes.SPREADSHEETS
);
```

---

## ⚙️ Endpoints

### 1. `GET /api/email/auth?username={gmail}`

- Triggers Google OAuth consent page
- Responds with:
  ```json
  { "auth_url": "https://..." }
  ```

### 2. `GET /Callback?code=...&state={gmail}`

- Google redirects here after login
- Saves credentials scoped to that `gmail` user

### 3. `POST /api/email/validate-email`

- Validate all emails in the sheet that are registered and make `AUTH_STATUS = Authorized/Not Authorized`
- Fill the `STATUS = READY/AUTH_URL`
- Updates Sheet columns:
  - `SENT_DATE`
  - `STATUS = SENT`
  - (optional: `TRACKING_ID`)

### 4. `GET /api/email/send-email`

- Bulk-sends emails for rows with `STATUS = READY`
- Updates Sheet columns:
  - `TRACKING_STATUS = SENT`
  - `STATUS = SENT`
  - `EMAIL_COUNT = +1`
  - (optional: `TRACKING_ID`)

**Example Curl**
```bash
curl -X POST http://localhost:8080/api/email/send-email
```

---

## ✉️ Google Sheet Format

Here’s an improved and polished version of that line for your `README.md`:

---

```
| A: SENT_DATE | B: USERNAME | C: GMAIL | D: COMPANY | E: COMPANY_GMAIL | F: ROLE | G: TEMPLATE | H: STATUS | I: AUTH_STATUS | J: TRACKING_STATUS | K: EMAIL_COUNT |
```

* ✅ `H: STATUS = READY`: Marks the row as eligible for sending.
* ✉️ Java reads the row, sends an email using the sender's Gmail, and updates:

  * `A: SENT_DATE` → with the timestamp
  * `H: STATUS` → to `SENT`
  * `J: TRACKING_STATUS` → to track if the email was opened
  * `K: EMAIL_COUNT` → number of times email was sent
* 🔒 `J: TRACKING_STATUS` and `K: EMAIL_COUNT` are **protected columns** — editable only by the Java program, not manually by users.

---

## 📦 How Email Is Sent

1. Filter rows where `STATUS = READY`
2. Map each to `EmailDTO`
3. Generate email body with:

```java
String body = EmailTemplateRenderer.renderTemplate(dto.getTemplate(), dto);
```

4. Send email:
```java
Gmail service = getGmailService(dto.getGmail());
sendMessage(service, "me", mimeMessage);
```

5. Update row in Sheet with:
   - `SENT_DATE = today`
   - `STATUS = SENT`
   - `TRACKING_ID = {uuid} (work in progress)` 

---

## 🛡️ Optional Email Open Tracking (WIP)

Add this to HTML email body:

```html
<img src="https://your-app.com/track?tid={{TRACKING_ID}}" width="1" height="1" style="display:none">
```

Implement `/track` endpoint in Spring Boot to mark `STATUS = READ` in Sheet.

---

## 🧩 Tech Stack

| Library / Tool           | Purpose                        |
|--------------------------|---------------------------------|
| Spring Boot              | REST API, scheduling           |
| Google API Java Client   | Gmail & Sheets integration     |
| JavaMail (Jakarta Mail)  | Build & send MimeMessage       |
| Google Sheets v4         | Read/update spreadsheet data   |
| Google OAuth2 Client     | Secure Gmail auth per user     |

---

## 🪁 Usage Flow

1. Start Spring app: `mvn spring-boot:run`
2. Authorize Gmail:  
   - `GET /api/email/auth?username=you@gmail.com` → open returned link  
   - Google redirects to `/Callback?code=…&state=you@gmail.com`
3. Fill your Sheet with data & set `STATUS = READY` rows
4. Trigger email sending:  
   - `GET /api/email/send-email`
5. Check your sheet:  
   - `STATUS` becomes `SENT`, `SENT_DATE` & `TRACKING_ID` set
6. *(Optional)* When recipient opens the email, `/track?tid=…` marks `STATUS = READ`

---

## 🚀 Tips & Next Steps

- Add scheduler to auto-run the send endpoint
- Validate data before sending
- Enhance with a front-end for authorization & stats
- Use protected range in Sheet for `TRACKING_ID` column
- Store tokens securely (DB)
- Add retry logic for failed emails or API errors

---

## ❤️ Contributing

Pull requests and ⭐ are welcome! 🥳

1. Fork it  
2. Create feature branch  
3. Commit & push  
4. Open PR

---

## 🔗 Reach Out

- Email: `pushpajitnexus007@gmail.com`  
- LinkedIn: `https://www.linkedin.com/in/pushpajit-biswas-6928b715b/`

---

### 🎉 Happy Cold Mailing!
