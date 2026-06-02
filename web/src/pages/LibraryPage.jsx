import { useEffect, useState } from "react";
import { getLibrary } from "../service/gameService";

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

    return(
        <div>
            <h1>Biblioteca</h1>

            {games.map(game => (
                <div key={game.id}>
                    <h3>{game.name}</h3>
                    <p>Status: {game.status}</p>
                    <p>Nota: {game.rating}</p>
                    <img src={game.imageUrl} alt={game.name} width={200} />
                </div>
            ))}
        </div>
    )
}