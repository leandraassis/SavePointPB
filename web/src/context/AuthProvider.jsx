import { useEffect, useState } from "react";
import { login, logout, refresh, register, setSessionExpiredHandler } from "../service/authService";
import { AuthContext } from "./AuthContext";

export default function AuthProvider({ children }) {

    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        setSessionExpiredHandler(() => setUser(null));

        async function restoreSession() {
            try {
                const result = await refresh();
                setUser(result);
            } catch {
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        }

        restoreSession();
    }, []);

    async function signIn(credentials) {
        const result = await login(credentials);
        setUser(result);
    }

    async function signUp(newUser) {
        const result = await register(newUser);
        setUser(result);
    }

    async function signOut() {
        try {
            await logout();
        } catch(error) {
            console.error(error);
        } finally {
            setUser(null);
        }
    }

    return(
        <AuthContext.Provider value={{ user, isLoading, signIn, signUp, signOut }}>
            {children}
        </AuthContext.Provider>
    );
}
