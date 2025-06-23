import { MentorDashboard } from './components/MentorDashboard';
import { EntrepreneurDashboard } from './components/EntrepreneurDashboard';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Home } from './components/Home';
import { Login } from './components/Login/Login';
import { ResetPasswordPage } from './components/Login/pages/ResetPasswordPage.tsx'
import NavBarWrapper from './components/NavBar/NavBarWrapper';
import Footer from './components/Footer/Footer.tsx';
import About from './pages/About/About.tsx';
import './App.css'


function App() {

  return (
    <Router>
      <div className='app-container'>
        <NavBarWrapper />
        <main className='app-main-content'>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/home" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/dashboard" element={<EntrepreneurDashboard />} />
            <Route path="/mentor-dashboard" element={<MentorDashboard />} />
          </Routes>
        </main>
        <Footer />
      </div>

    </Router>
  );
}

export default App;
