import AppRouter from "./router/AppRouter";
import AuthProvider from "./context/AuthProvider";

export default function App() {
    return(
        <AuthProvider>
            <AppRouter />
        </AuthProvider>
    );
}