import React, { useState } from 'react';
import NavBar from './components/NavBar/NavBar';
import userImg from './assets/userIcon.png';
import './App.css'

/* Este es el ejemplo original de cómo se podría estructurar el componente App en React.
function App() {
*/

const App: React.FC = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const navLinks = [
    { label: "Inicio", url: "#" },
    { label: "Perfil", url: "#" },
    { label: "Servicios", url: "#" },
    { label: "Cerrar Sesión", url: "#" },
  ];

  return (
    <>
      <div>
        <NavBar userIcon={userImg} links={navLinks} isLoggedIn={isLoggedIn} />
      </div>

      {/* Botón para simular iniciar/cerrar sesión */}
      <div style={{ padding: '2rem' }}>
        <button onClick={() => setIsLoggedIn(prev => !prev)}>
          {isLoggedIn ? "Cerrar sesión" : "Iniciar sesión"}
        </button>
      </div>
    </>
  );
};

export default App;
