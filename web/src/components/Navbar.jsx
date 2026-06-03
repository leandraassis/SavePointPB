import { NavLink } from "react-router-dom";
import "../style/Navbar.css"

export default function Navbar() {
    return(
        <nav>
            <h1>SavePoint</h1>

            <div>
                <NavLink to="/">Buscar jogos</NavLink>
                <NavLink to="/library">Biblioteca</NavLink>
            </div>
        </nav>
    )
}