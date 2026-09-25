const BASE_URL = import.meta.env.VITE_AUTH_URL;

let accessToken = null;
let refreshPromise = null;
let onSessionExpired = null;

export function getAccessToken() {
    return accessToken;
}

export function setSessionExpiredHandler(handler) {
    onSessionExpired = handler;
}

async function authRequest(path, body) {
    const response = await fetch(`${BASE_URL}${path}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: body ? JSON.stringify(body) : undefined
    });

    if(!response.ok) throw new Error(response.status);

    const result = await response.json();
    accessToken = result.accessToken;

    return result.user;
}

export async function register(user) {
    return authRequest("/register", user);
}

export async function login(credentials) {
    return authRequest("/login", credentials);
}

//reaproveita a mesma chamada se já houver um refresh em andamento: mandar o mesmo cookie duas vezes
export function refresh() {
    if(!refreshPromise) {
        refreshPromise = authRequest("/refresh")
            .catch(error => {
                accessToken = null;
                if(onSessionExpired) onSessionExpired();
                throw error;
            })
            .finally(() => refreshPromise = null);
    }

    return refreshPromise;
}

export async function logout() {
    accessToken = null;

    await fetch(`${BASE_URL}/logout`, {
        method: "POST",
        credentials: "include"
    });
}
