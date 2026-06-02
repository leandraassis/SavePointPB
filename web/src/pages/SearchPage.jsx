import { useState } from "react"
import { searchGames } from "../service/gameService";

export default function SearchPage() {

    const [query, setQuery] = useState("");
    const [games, setGames] = useState([]);

    async function handleSearch() {
        if(!query.trim()) return;

        try {
            const result = await searchGames(query);
            setGames(result);
        } catch(error) {
            console.error(error)
        }
    }
    
    return(
        <div>
            <h1>Pesquisar jogos</h1>
            <input type="text" 
            value={query} 
            onChange={(e) => setQuery(e.target.value)} 
            placeholder="Digite um jogo..." 
            />

            <button onClick={handleSearch}>
                Buscar
            </button>

            <hr />

            {games.map(game => (
                <div key={game.rawgId}>
                    <h3>{game.name}</h3>
                    <img src={game.imageUrl} alt={game.name} width={200} />
                </div>
            ))}
        </div>
    );
}