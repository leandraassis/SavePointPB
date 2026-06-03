import "../style/GameCard.css"

export default function GameCard({ game, children }) {
    return(
        <div className="card">
            <img src={game.imageUrl} alt={game.name} />
            <div className="card-body">
                
                <h3>{game.name}</h3>
                {game.status &&<p>Status: {game.status}</p>}
                {game.rating && <p>Nota: {game.rating}</p>}
                
                <div className="card-actions">
                    {children}
                </div>
            </div>
        </div>
    );
}