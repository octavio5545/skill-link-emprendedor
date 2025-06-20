import { useState } from 'react';
import NavBar from './NavBar';
import userImg from '../../assets/userIcon.png';

const NavBarWrapper = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const navLinks = [
    { label: "Dashboard", url: "/dashboard" },
    { label: "Cerrar Sesión", url: "/", onClick: () => setIsLoggedIn(false) },
  ];

  return (
    <>
      <NavBar userIcon={userImg} links={navLinks} isLoggedIn={isLoggedIn} />

      {/* Botón temporal para simular login/logout */}
      <div style={{ padding: '2rem' }}>
        <button onClick={() => setIsLoggedIn(prev => !prev)}>
          {isLoggedIn ? "Cerrar sesión" : "Iniciar sesión"}
        </button>
      </div>
    </>
  );
};

export default NavBarWrapper;
