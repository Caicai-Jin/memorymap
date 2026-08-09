# MemoryMap — Learning Notes

Running notes from walking through the codebase step by step. Newest entries added at the bottom.

---

## How Spring Boot decides which method handles a request

Two separate things happen — one **once, at startup**, and one **every time a request arrives**. Keeping these separate is the key to understanding it.

### Part A — Once, when the app starts (`MemorymapApplication.main`)

1. Spring Boot starts an embedded Tomcat server, which stands at door #8080 and listens for network traffic.
2. Spring scans the whole codebase once, looking for every `@RestController` class and every `@PostMapping` / `@GetMapping` / etc. method inside them.
3. For each one found, it writes an entry into an internal lookup table it builds in memory — something like:

- `/login` + `POST` → `AuthController.loginUser`
- `/register` + `POST` → `AuthController.createUser`
- `/moments` + `GET` → `MomentController.getAllMoments`
- ...and so on, one row per `@...Mapping` in the project

This table is built **the moment the app starts up** — before the React app has even loaded in a browser, before anyone has sent a single request.

Analogy: a company printing its internal phone directory each morning before opening — done once, in advance, not re-created for every phone call.

### Part B — Every time an actual request arrives

1. Tomcat receives the raw bytes from a `fetch` call (e.g. from `Login.jsx`) and parses them into a structured request: path, method, headers, body.
2. Tomcat hands the parsed request to one single Spring component that catches **every** incoming request: the `DispatcherServlet`. Think of it as the one receptionist who answers every phone call, no matter what the caller wants.
3. The `DispatcherServlet` doesn't search source files or "think" about it — it takes the pair `(path, method)` off the request and looks it up in the table from Part A. That's a fast lookup, like flipping to a page in an already-built index, not a live search through code.
4. The table says who owns that pair (e.g. `AuthController.loginUser`), so the `DispatcherServlet` calls that exact method, handing it the request — which is where `@RequestBody` + Jackson step in to build the Java object from the JSON body.

### One-line takeaway

Spring didn't "decide" anything at the moment the request arrived — it had already decided, in advance, at startup, by reading the `@...Mapping` annotations and building a lookup table. When a real request shows up, all that's left is a quick lookup in that pre-built table, not a live search through the project.

That's also why a typo like `@PostMapping("/logn")` breaks things immediately at the URL level — the table would simply never contain an entry for `/login`, and Tomcat would reply `404 Not Found` before any Java code in the method body ever ran.

---

## `<Route>`, `path`, and `element` in React Router

- `<Route>` — a component from `react-router-dom` that doesn't render anything by itself; it's just one entry in a list, telling the parent `<Routes>` "if the browser's URL matches this, show this." `<Routes>` scans all its `<Route>` children and renders only the one that matches.
- `path="/moments"` — a plain string prop: the URL this entry matches against. `Routes` compares the browser's current address bar against every `path` in the list to find the one to render.
- `element={...}` — a prop whose value is a piece of JSX (not a string) — literally the component instance to render when `path` matches. It's written as `element={<Something />}` because it needs an actual React element, not just the component's name as text.

So the full line:

```jsx
<Route path="/moments" element={<ProtectedRoute><Moments /></ProtectedRoute>} />
```

reads as: "when the URL is `/moments`, render this exact JSX tree" — which happens to be `Moments` wrapped in `ProtectedRoute`.
