import { MentorDashboard } from './components/MentorDashboard';
import { EntrepreneurDashboard } from './components/EntrepreneurDashboard';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Home } from './components/Home';
import { Login } from './components/Login/Login';
import { ResetPasswordPage } from './components/Login/pages/ResetPasswordPage.tsx'
import NavBarWrapper from './components/NavBar/NavBarWrapper';
import Footer from './components/Footer/Footer.tsx';
import { AddPost } from './components/Home/AddPost.tsx';
import './App.css'


function App() {

  return (
    <Router>
      <NavBarWrapper />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/home" element={<Home />} />
        <Route path="/add-post" element={<AddPost />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/dashboard" element={<EntrepreneurDashboard />} />
        <Route path="/mentor-dashboard" element={<MentorDashboard />} />
      </Routes>
      <Footer />
    </Router>
  );
}

export default App;
