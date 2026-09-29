import { useEffect, useState } from "react";
import { deleteGame, getLibrary, updateGame } from "../service/gameService";
import GameCard from "../components/GameCard";
import Navbar from "../components/Navbar";
import EditGameModal from "../components/EditGameModal";
import "../style/LibraryPage.css"

export default function LibraryPage() {

    const [games, setGames] = useState([]);
    const [selectedGame, setSelectedGame] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    
    //update jogo e handlers do modal
    function handleOpenModal(game) {
        setSelectedGame(game);
        setIsModalOpen(true);
    }

    function handleCloseModal() {
        setSelectedGame(null);
        setIsModalOpen(false);
    }

    async function handleSave(updatedData) {
        try {
            const updatedGame = await updateGame(selectedGame.id, updatedData);

            setGames(current => current.map(game =>
                game.id === updatedGame.id ? updatedGame : game
            ));

            handleCloseModal();
            
        } catch(error) {
            console.error(error);
        }
    }

    //carregar biblioteca
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

    //deletar jogo
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
                            <button onClick={() => handleOpenModal(game)}>editar</button>
                            <button onClick={() => handleDelete(game.id)}>excluir</button>
                        </GameCard>
                    ))}
                </div>
                <EditGameModal key={selectedGame?.id} isOpen={isModalOpen} game={selectedGame}
                onClose={handleCloseModal} onSave={handleSave}/>
            </div>
            
        </>
    )
}