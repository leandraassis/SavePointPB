import { useState } from "react"
import { addGame, searchGames } from "../service/gameService";
import GameCard from "../components/GameCard";
import Navbar from "../components/Navbar";
import "../style/SearchPage.css"

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

    async function handleAddGame(rawgId) {
        try {
            await addGame({ rawgId, status: "WISHLIST"});
            alert("Jogo adicionado com sucesso!");
        } catch(error) {
            console.error(error);
        }
        
    }
    
    return(
        <>
            <Navbar />
            
            <div className="search-page">
                <h1>Pesquisar jogos</h1>

                <div className="search-controls">
                    <input type="text" 
                    value={query} 
                    onChange={(e) => setQuery(e.target.value)} 
                    placeholder="Digite um jogo..." 
                    />

                    <button onClick={handleSearch}>
                        Buscar
                    </button>
                </div>
                
                <hr />

                <div className="cards-grid">
                    {games.map(game => (
                        <GameCard key={game.rawgId} game={game}>
                            <button onClick={() => handleAddGame(game.rawgId)}>adicionar</button>
                        </GameCard>
                    ))}
                </div>
            </div>
        </>
    );
}