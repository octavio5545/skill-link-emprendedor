import { MentorDashboard } from './components/MentorDashboard';
import { EntrepreneurDashboard } from './components/EntrepreneurDashboard';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Login } from './components/Login/Login';
import { ResetPasswordPage } from './components/Login/pages/ResetPasswordPage.tsx'

import './App.css'

function App() {

  return (
    <Router>      
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/dashboard" element={<EntrepreneurDashboard />} />
        <Route path="/mentor-dashboard" element={<MentorDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
