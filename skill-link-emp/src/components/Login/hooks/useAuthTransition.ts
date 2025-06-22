import { useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { forgotPassword } from '../api/auth';
import type { AuthMode, FormData, UserRole, UserInterest } from '../types/auth';

export const useAuthTransition = () => {
    const navigate = useNavigate();
    const [authMode, setAuthMode] = useState<AuthMode>('login');
    const [isTransitioning, setIsTransitioning] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const [formData, setFormData] = useState<FormData>({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        role: '',
        interests: []
    });

    const [apiMessage, setApiMessage] = useState<string | null>(null);
    const [isError, setIsError] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const passwordValidation = useMemo(() => {
        if (authMode !== 'register') {
            return { isValid: true, checks: [] };
        }

        const checks = [
            {
                id: 'length',
                label: 'Mín. 8 caracteres',
                isValid: formData.password.length >= 8
            },
            {
                id: 'digit',
                label: 'Un dígito',
                isValid: /\d/.test(formData.password)
            },
            {
                id: 'special',
                label: 'Un caracter especial',
                isValid: /[!@#$%^&*(),.?":{}|<>]/.test(formData.password)
            }
        ];

        const isValid = checks.every(check => check.isValid);

        return { isValid, checks };
    }, [formData.password, authMode]);

    const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
    }, []);

    const handleRoleChange = useCallback((role: UserRole) => {
        setFormData(prev => ({
            ...prev,
            role
        }));
    }, []);

    const handleInterestToggle = useCallback((interest: UserInterest) => {
        setFormData(prev => ({
            ...prev,
            interests: prev.interests.includes(interest)
                ? prev.interests.filter(i => i !== interest)
                : [...prev.interests, interest]
        }));
    }, []);

    const resetForm = useCallback(() => {
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            password: '',
            role: '',
            interests: []
        });
    }, []);

    const switchMode = useCallback((mode: AuthMode) => {
        setIsTransitioning(true);

        setTimeout(() => {
            setAuthMode(mode);
            resetForm();
            setShowPassword(false);
            setApiMessage(null);
            setIsError(false);
            setIsLoading(false);

            setTimeout(() => {
                setIsTransitioning(false);
            }, 100);
        }, 600);
    }, [resetForm]);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        setApiMessage(null);
        setIsError(false);
        setIsLoading(true);

        try {
            if (authMode === 'register') {
                if (!formData.role) {
                    setApiMessage('Por favor selecciona un rol.');
                    setIsError(true);
                    setIsLoading(false);
                    return;
                }

                if (formData.interests.length === 0) {
                    setApiMessage('Por favor, selecciona al menos un interés.');
                    setIsError(true);
                    setIsLoading(false);
                    return;
                }

                if (!passwordValidation.isValid) {
                    setApiMessage('La contraseña no cumple con los requisitos.');
                    setIsError(true);
                    setIsLoading(false);
                    return;
                }

                setApiMessage('¡Registro exitoso! Redirigiendo...');
                setIsError(false);
                resetForm();
                
                setTimeout(() => {
                    navigate('/home');
                }, 1500);
                
            } else if (authMode === 'login') {
                setApiMessage('¡Inicio de sesión exitoso! Redirigiendo...');
                setIsError(false);
                resetForm();
                
                setTimeout(() => {
                    navigate('/home');
                }, 1500);
                
            } else if (authMode === 'forgot') {
                if (!formData.email) {
                    setApiMessage('Por favor ingresa tu correo electrónico.');
                    setIsError(true);
                    setIsLoading(false);
                    return;
                }

                const response = await forgotPassword(formData.email);
                setApiMessage(response.mensaje);
                setIsError(!response.exito);
                
                if (response.exito) {
                    setFormData(prev => ({ ...prev, email: '' }));
                }
            }
        } catch (error: any) {
            setApiMessage(error.message || 'Ocurrió un error inesperado. Inténtalo de nuevo.');
            setIsError(true);
        } finally {
            setIsLoading(false);
        }
    }, [authMode, formData, resetForm, passwordValidation.isValid, navigate]);

    return {
        authMode,
        formData,
        isTransitioning,
        showPassword,
        setShowPassword,
        handleInputChange,
        handleRoleChange,
        handleInterestToggle,
        handleSubmit,
        switchMode,
        apiMessage,
        isError,
        isLoading,
        passwordValidation
    };
};