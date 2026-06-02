export default function GameCard({ game, children }) {
    return(
        <div>
            <img src={game.imageUrl} alt={game.name} width={200} />
        </div>
    )
}