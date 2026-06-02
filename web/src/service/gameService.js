const BASE_URL = import.meta.env.VITE_API_URL;

export async function searchGames(query) {
    const response = await fetch(`${BASE_URL}/search?query=${encodeURIComponent(query)}`);

    if(!response.ok) throw new Error("Failed to search games");
    
    return response.json();
}

export async function getLibrary() {
    const response = await fetch(BASE_URL);

    if(!response.ok) throw new Error("Failed to load library");

    return response.json();
}

export async function addGame(game) {
    const response = await fetch(BASE_URL, {
        method: "POST",
        headers: { 
            "Content-Type": "application/json"
        },
        body: JSON.stringify(game)
    });

    if(!response.ok) throw new Error("Failed to add game");

    return response.json();
}

export async function updateGame(id, game) {
    const response = await fetch(`${BASE_URL}/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(game)
    });

    if(!response.ok) throw new Error("Failed to update game");

    return response.json();
}

export async function deleteGame(id) {
    const response = await fetch(`${BASE_URL}/${id}`, {
        method: "DELETE"
    });

    if(!response.ok) throw new Error("Failed to delete game");
}