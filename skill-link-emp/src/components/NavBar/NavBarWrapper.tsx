import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavBar from './NavBar';
import userImg from '../../assets/userIcon.png';

const NavBarWrapper = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const navigate = useNavigate();

    const navLinks = [
        {
            label: "Inicio",
            url: "/home"
        },
        {
            label: "Dashboard",
            url: "/dashboard"
        },
        {
            label: "Cerrar Sesión",
            url: "/", onClick: () => setIsLoggedIn(false)
        },
    ];

    const handleLogin = () => {
        setIsLoggedIn(true);
        navigate('/dashboard');
    };

    return (
        <>
            <NavBar
                userIcon={userImg}
                links={navLinks}
                isLoggedIn={isLoggedIn}
                onLogin={handleLogin}
            />
        </>
    );
};

export default NavBarWrapper;
