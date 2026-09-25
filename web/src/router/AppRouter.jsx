import { BrowserRouter, Routes, Route } from "react-router-dom";
import SearchPage from "../pages/SearchPage";
import LibraryPage from "../pages/LibraryPage";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ProtectedRoute from "./ProtectedRoute";

export default function AppRouter() {
    return(
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/" element={<ProtectedRoute><SearchPage /></ProtectedRoute>} />
                <Route path="/library" element={<ProtectedRoute><LibraryPage /></ProtectedRoute>} />
            </Routes>
        </BrowserRouter>
    );

}