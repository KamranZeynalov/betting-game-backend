# 🎲 Betting Game Backend

A Spring Boot backend for a real-time betting game, where players submit a bet and a number (1–100) via WebSocket. The server generates a random number in response and calculates the win amount based on the player's chance of winning.

> Originally built as a technical assignment.

---

## 🚀 Tech Stack

- Java 11
- Spring Boot 2.x
- WebSocket (Spring Messaging)
- Bean Validation (JSR-380)
- JUnit 5 & Spring Test
- Gradle

---

## 🧠 Game Logic

1. The player sends:
   - A **nickname**
   - A **whole number** between 1 and 100
   - A **bet amount** (positive decimal)

2. The server generates a **random number (1–100)**.

3. If the player's number is **greater** than the server's, the win is calculated as:
    **win = bet × (99 / (100 - number))**


> 🧮 Example:  
> Player chooses `50`, bets `40.5`  
> `win = 40.5 × (99 / (100 - 50)) = 80.19`

4. The result is returned back to the player via WebSocket.

---

## 🔌 WebSocket Communication

### 🧑 Client to Server (Input)

**Message Format:**
```json
{
  "nickname": "username",
  "number": 50,
  "bet": 40.5
}
```

Send this over WebSocket to:

```bash
ws://localhost:8080/game
```

### 📩 Server to Client (Response)

**On Success:**
```json
{
  "serverNumber": 32,
  "win": 80.19,
  "status": "WON"
}
```

**On Loss:**
```json
{
  "serverNumber": 89,
  "win": 0.0,
  "status": "LOST"
}
```

**On Error (e.g., nickname taken):**
```json
{
  "error": "Nickname already taken"
}
```

---
## 🧪 Testing
Run all tests:

```bash
./gradlew test
```

Coverage includes:

- 🎮 Core game logic: betting mechanics and win calculation
- 🔌 WebSocket handling for:
  - A single player session
  - Multiple concurrent player sessions
- ✅ Input validation and error handling:
  - Duplicate nickname detection
  - Invalid input rejection

---
## 📈 RTP Simulation (Not Implemented)

The original assignment included a requirement to:

> Simulate 1 million rounds using 24 threads to calculate **Return to Player (RTP)**.

⚠️ This feature has **not been implemented** and is marked as a possible improvement.

---

## 🛠️ Run Locally

To start the backend:

```bash
./mvnw spring-boot:run
```
You can now connect a WebSocket client (e.g., browser, Postman, or frontend app) to:
```bash
ws://localhost:8080/game
```

---

## 🔐 Input Validation

The backend enforces strict validation rules to ensure fair gameplay:

- 🧑 Nickname must be **unique per connection**
- 🔢 Number must be between **1 and 100**
- 💰 Bet must be a **positive decimal**
- ❌ Invalid inputs or incorrect game state will trigger **WebSocket error messages**

---

## 🚀 Possible Improvements

Here are some enhancements that can be implemented in future iterations:

- ✅ Add RTP simulation logic and expose statistics
- 📊 Create a `/stats` REST or WebSocket endpoint for aggregated game data
- 🧪 Improve test coverage for:
  - Invalid JSON
  - Malformed WebSocket messages
- 🖥️ Build an optional frontend UI to demonstrate the game flow

---
## 📬 Contact

Built by **Kamran Zeynalov**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-blue?logo=linkedin&style=flat-square)](https://www.linkedin.com/in/zeynalov-kamran/)

