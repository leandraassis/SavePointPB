import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../style/Navbar.css"

export default function Navbar() {
    const { user, signOut } = useAuth();

    return(
        <nav>
            <h1>SavePoint</h1>

            <div>
                <NavLink to="/">Buscar jogos</NavLink>
                <NavLink to="/library">Biblioteca</NavLink>
            </div>

            <div className="nav-user">
                <span>{user.username}</span>
                <button onClick={signOut}>Sair</button>
            </div>
        </nav>
    )
}