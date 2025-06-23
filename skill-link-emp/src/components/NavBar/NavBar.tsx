import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { Lightbulb } from 'lucide-react';
import './NavBar.css';

interface Props {
    userIcon: string;
    links: { label: string, url: string; onClick?: () => void }[];
    isLoggedIn: boolean;
    onLogin?: () => void;
}

// Muestra el menú desplegable y define estado de los clics.
const NavBar = ({ userIcon, links, isLoggedIn, onLogin }: Props) => {
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

      /** Cierra el menú y ejecuta la acción adicional si existe */
    const handleMenuItemClick = (onClick?: () => void) => {
        if (onClick) onClick();
        setIsOpen(false);
    };

    return (
        <nav className="navbar">
            <Link to="/home">
                <section className="navbar-wrapper">
                    <div className="navbar-logo__container">
                        <Lightbulb />
                    </div>
                    <div className='navbar-title__container'>
                        <h1 className="navbar-title">SkillLink</h1>
                        <h2 className="navbar-subtitle">Emprendedor</h2>
                    </div>
                </section>
            </Link>
            <div className="menu" ref={menuRef}>
                {!isLoggedIn && (
                    <>
                        <button className="menu-session" onClick={onLogin}>Iniciar Sesión</button>
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
                                <Link
                                    to={link.url}
                                    onClick={() => handleMenuItemClick(link.onClick)}
                                >
                                    {link.label}
                                </Link>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </nav>
    );
};

export default NavBar;
