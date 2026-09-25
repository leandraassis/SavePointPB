import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../style/AuthPage.css";

export default function RegisterPage() {

    const { user, signUp } = useAuth();
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    if(user) return <Navigate to="/" replace />;

    async function handleSubmit(e) {
        e.preventDefault();

        try {
            await signUp({ username, email, password });
        } catch(error) {
            if(error.message === "409") {
                alert("Esse e-mail já está cadastrado.");
            } else if(error.message === "400") {
                alert("Dados inválidos. Confira os campos.");
            } else {
                alert("Erro ao criar conta.");
            }
            console.error(error);
        }
    }

    return(
        <div className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1>SavePoint</h1>
                <h2>Criar conta</h2>

                <input type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Nome de usuário"
                maxLength="50"
                required
                />

                <input type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="E-mail"
                required
                />

                <input type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Senha (mínimo 8 caracteres)"
                minLength="8"
                maxLength="72"
                required
                />

                <button type="submit">Cadastrar</button>

                <p>Já tem conta? <Link to="/login">Entrar</Link></p>
            </form>
        </div>
    );
}
