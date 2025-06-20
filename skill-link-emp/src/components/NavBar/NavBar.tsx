import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import './NavBar.css';

interface Props {
    userIcon: string;
    links: { label: string, url: string; onClick?: () => void }[];
    isLoggedIn: boolean;
}

/** Muestra el menú desplegable y define estado de los clics. */
const NavBar = ({ userIcon, links, isLoggedIn }: Props) => {
    const [isOpen, setIsOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);
    const isOpenRef = useRef(isOpen);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (isOpenRef.current && menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    useEffect(() => {
        // Sincroniza la referencia con el estado actual
        isOpenRef.current = isOpen;
    }, [isOpen]);

    useEffect(() => {
        if (!isLoggedIn) {
            setIsOpen(false);
        }
    }, [isLoggedIn]);

    const handleToggle = () => {
        setIsOpen((prev) => !prev);
    };

    return (
        <nav className="navbar">
            <h1 className="navbar-title">Logo</h1>
            <div className="menu" ref={menuRef}>
                {!isLoggedIn && (
                    <>
                        <button className="menu-session">Iniciar Sesión</button>
                        <button className="menu-register">Regístrate</button>
                    </>
                )}

                {isLoggedIn && (
                    <button className="dropdown-toggle" onClick={handleToggle}>
                        <img src={userIcon} alt="Imagen de usuario" className="dropdown-toggle-icon" />
                    </button>
                )}
                {isLoggedIn && isOpen && (
                    <ul className="dropdown-menu">
                        {links.map((link, index) => (
                            <li key={index}>
                                <Link to={link.url} onClick={link.onClick}>{link.label}</Link>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </nav>
    );
};

export default NavBar;
