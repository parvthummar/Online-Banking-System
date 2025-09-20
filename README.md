# 🏦 Online Banking Full-Stack Application

This is a **web-based full-stack online banking system** built with:

- **Backend:** Java + Spring Boot (Spring Security, JPA/Hibernate, JWT authentication, MySQL)  
- **Frontend:** React + Redux (Single Page Application with Material UI)  

The project simulates a **real-world digital banking platform**, allowing users to:  
- Register and log in securely (JWT-based authentication)  
- Open new accounts  
- View transaction history and account statements  
- Transfer funds between accounts  
- Deposit and withdraw money  
- Make payments (e.g., bills)  
- Get **real-time charts and account flow visualization** via Redux state updates  

---

## ⚙️ Backend (Spring Boot)

- Main class: `DemoBankV1Application.java`  
- REST APIs for:
  - User Authentication (`AuthController`)  
  - Account Management (`AccountController`)  
  - Transactions (`TransactController`)  
  - Payments (`PaymentController`)  
- **Database Integration:** MySQL with Spring Data JPA repositories  
- **Security:** JWT tokens for authentication, Interceptors for request validation  
- **Utilities:** Auto-generate account numbers, send confirmation emails (`MailConfig`, `MailMessenger`)  
- **Error Handling:** `GlobalExceptionHandler` for clean API error responses  

### 🚀 Run Backend
```bash
cd "Online Banking App Spring Boot"
mvn clean install
mvn spring-boot:run

front end

cd frontend
npm install
npm start

