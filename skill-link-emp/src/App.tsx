import { MentorDashboard } from './components/MentorDashboard';
import { EntrepreneurDashboard } from './components/EntrepreneurDashboard';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Home } from './components/Home';

import { useState } from 'react';
import NavBar from './components/NavBar/NavBar';
import userImg from './assets/userIcon.png';
import './App.css'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const navLinks = [
    { label: "Dashboard", url: "/dashboard" },
    { label: "Cerrar Sesión", url: "/", onClick: () => setIsLoggedIn(false) },
  ];

  return (
    <Router>
      <NavBar userIcon={userImg} links={navLinks} isLoggedIn={isLoggedIn} />

      {/* Mock para simular iniciar/cerrar sesión */}
      <div style={{ padding: '2rem' }}>
        <button onClick={() => setIsLoggedIn(prev => !prev)}>
          {isLoggedIn ? "Cerrar sesión" : "Iniciar sesión"}
        </button>
      </div>

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/dashboard" element={<EntrepreneurDashboard />} />
        <Route path="/mentor-dashboard" element={<MentorDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
