import { useState } from "react";
import { GAME_STATUS_OPTIONS } from "../constants/gameStatus";
import "../style/EditGameModal.css";

export default function EditGameModal({ isOpen, game, onClose, onSave }) {

    const [status, setStatus] = useState(game?.status ?? "");
    const [rating, setRating] = useState(game?.rating ?? "");

    if(!isOpen) return null;

    function handleSaveClick() {
        const payload = {status};
        if(rating !== "") payload.rating = Number(rating);

        onSave(payload);
    }

    return(
        <div className="modal-overlay">
            <div className="modal">
                <h2>Editar jogo</h2>
                <p>{game.name}</p>

                <select value={status} onChange={(e) => setStatus(e.target.value)}>
                    {GAME_STATUS_OPTIONS.map(option => (
                        <option key={option} value={option}>
                            {option}
                        </option>
                    ))}
                </select>

                <input type="number" min="1" max="5" value={rating} 
                onChange={(e) => setRating(e.target.value)} />

                <div className="modal-actions">
                    <button onClick={handleSaveClick}>Salvar</button>
                    <button onClick={onClose}>Cancelar</button>
                </div>
            </div>
        </div>
    );
}