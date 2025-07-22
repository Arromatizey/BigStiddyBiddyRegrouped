# 📋 Backend API Suggestions

## ✅ RESOLVED: Room Creation Now Working

### **Solution Applied (Frontend Only)**
The room creation was failing because the frontend wasn't sending the required `owner` field. Fixed by:

1. **Added owner to payload**:
```javascript
owner: {
  id: userId  // Just the ID is enough, backend loads the full User
}
```

2. **Fixed UUID response handling**:
```typescript
// Backend returns quoted UUID: "123e4567-e89b-12d3-a456-426614174000"
return this.http.post(`${this.baseUrl}/rooms`, room, { responseType: 'text' });
```

---

## 🚨 CRITICAL: Endpoint getAllRooms Returns 404

### **Issue Details**
- **Endpoint**: `GET /api/rooms/getAllRooms`
- **Frontend Call**: `RoomsService.getRooms()`
- **Current Status**: ❌ **404 Not Found**
- **Expected Response**: `List<Room>`

### **Root Cause Analysis**
The backend code shows `@GetMapping("/getAllRooms")` exists in `RoomController`, but the server is returning 404. This suggests:

1. **Server restart needed** with latest code
2. **Profile mismatch** (dev vs prod security)
3. **Authentication required** but missing in test calls

### **Required Backend Action**
```bash
# Restart Spring Boot server
mvn spring-boot:run
```

**Verify endpoint exists:**
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/rooms/getAllRooms
```

---

## 🔧 Previously Identified Issues

### 1. **CORS Port Mismatch** 
**Status:** ✅ **RESOLVED** (Frontend now on port 4200)  

### 2. **Missing CORS Configuration**
**Status:** ⚠️ **STILL NEEDED**  
**Affected Controllers:**
- `SessionController.java` - Add `@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})`
- `DmController.java` - Add `@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})`
- `PromptController.java` - Add `@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})`

---

## 📊 Security Profile Issues

### 1. **Dev Profile Always Active**
**Status:** ⚠️ **WARNING**  
**Current:** `spring.profiles.active=dev`  
**Suggestion:** Use environment variables for production deployment 