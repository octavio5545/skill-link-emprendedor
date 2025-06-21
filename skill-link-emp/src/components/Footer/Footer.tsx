import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-content">
                <div className="footer-brand">
                    <h3>SkillLink</h3>
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
