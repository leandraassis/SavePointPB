import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../style/AuthPage.css";

export default function LoginPage() {

    const { user, signIn } = useAuth();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    if(user) return <Navigate to="/" replace />;

    async function handleSubmit(e) {
        e.preventDefault();

        try {
            await signIn({ email, password });
        } catch(error) {
            if(error.message === "401") {
                alert("E-mail ou senha inválidos.");
            } else {
                alert("Erro ao entrar.");
            }
            console.error(error);
        }
    }

    return(
        <div className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1>SavePoint</h1>
                <h2>Entrar</h2>

                <input type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="E-mail"
                required
                />

                <input type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Senha"
                required
                />

                <button type="submit">Entrar</button>

                <p>Não tem conta? <Link to="/register">Cadastre-se</Link></p>
            </form>
        </div>
    );
}
