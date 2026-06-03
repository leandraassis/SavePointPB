import { useEffect, useState } from "react";
import { deleteGame, getLibrary } from "../service/gameService";
import GameCard from "../components/GameCard";
import Navbar from "../components/Navbar";
import "../style/LibraryPage.css"

export default function LibraryPage() {

    const [games, setGames] = useState([]);

    useEffect(() => {
        async function loadGames() {
            try {
                const result = await getLibrary();
                setGames(result);
            } catch(error) {
                console.error(error);
            }
        }

        loadGames();
    }, []);

    async function handleDelete(id) {
        try {
            await deleteGame(id);
            setGames(current => current.filter(game => game.id !== id))
        } catch(error) {
            console.error(error);
        }
    }

    return(
        <>
            <Navbar />

            <div className="library-page">
                <h1>Biblioteca</h1>
                <hr />

                <div className="cards-grid">
                    
                    {games.map(game => (
                        <GameCard key={game.id} game={game}>
                            <button>editar</button>
                            <button onClick={() => handleDelete(game.id)}>excluir</button>
                        </GameCard>
                    ))}
                </div>
            </div>
        </>
    )
}