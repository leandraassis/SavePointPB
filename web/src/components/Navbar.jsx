export default function Navbar() {
    return(
        <nav>
            <h1>SavePoint</h1>

            <div>
                <Link to="/">Buscar jogos</Link>
                <Link to="/library">Biblioteca</Link>
            </div>
        </nav>
    )
}