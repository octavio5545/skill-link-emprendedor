import { Lightbulb } from 'lucide-react';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-content">
                <div className="footer-brand">
                    <div className="footer-brand__container">
                        <Lightbulb className="footer-icon" />
                        <h3>SkillLink</h3>
                    </div>
                    <p>Conectando talento con oportunidades.</p>
                </div>
                <div className="footer-links">
                    <a href="/about">Sobre Nosotros</a>
                    <a href="/contact">Contacto</a>
                    <a href="/privacy">Privacidad</a>
                    <a href="/terms">Términos</a>
                </div>
            </div>
            <div className="footer-bottom">
                <p>&copy; {new Date().getFullYear()} SkillLink. Casi todos los derechos reservados.</p>
            </div>
        </footer>
    );
};

export default Footer;
