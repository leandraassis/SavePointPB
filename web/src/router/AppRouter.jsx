import { BrowserRouter, Routes, Route } from "react-router-dom";
import SearchPage from "../pages/SearchPage";
import LibraryPage from "../pages/LibraryPage";

export default function AppRouter() {
    return(
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<SearchPage />} />
                <Route path="/library" element={<LibraryPage />} />
            </Routes>
        </BrowserRouter>
    );

}