import { Mentorship, Project, Session, Notification } from '../types';

export const mockMentorships: Mentorship[] = [
  {
    id: '1',
    mentee: {
      name: 'Ana García',
      avatar: 'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=100&h=100&fit=crop',
      project: 'EcoTech Solutions'
    },
    status: 'active',
    progress: 75,
    nextSession: '2025-01-20 14:00',
    hoursCompleted: 18,
    totalHours: 24
  },
  {
    id: '2',
    mentee: {
      name: 'Carlos Mendez',
      avatar: 'https://images.pexels.com/photos/1222271/pexels-photo-1222271.jpeg?auto=compress&cs=tinysrgb&w=100&h=100&fit=crop',
      project: 'FinanceApp Pro'
    },
    status: 'active',
    progress: 45,
    nextSession: '2025-01-21 10:00',
    hoursCompleted: 12,
    totalHours: 20
  },
  {
    id: '3',
    mentee: {
      name: 'Sofia Rodriguez',
      avatar: 'https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg?auto=compress&cs=tinysrgb&w=100&h=100&fit=crop',
      project: 'HealthTracker'
    },
    status: 'active',
    progress: 90,
    nextSession: '2025-01-22 16:00',
    hoursCompleted: 22,
    totalHours: 25
  }
];

export const mockProjects: Project[] = [
  {
    id: '1',
    name: 'AI Learning Platform',
    description: 'Plataforma de aprendizaje personalizado con IA',
    status: 'development',
    progress: 65,
    teamMembers: 4,
    mentor: {
      name: 'Dr. María López',
      avatar: 'https://images.pexels.com/photos/1181424/pexels-photo-1181424.jpeg?auto=compress&cs=tinysrgb&w=100&h=100&fit=crop',
      specialty: 'EdTech & AI'
    },
    daysUntilPitch: 45
  },
  {
    id: '2',
    name: 'Sustainable Fashion',
    description: 'Marketplace de moda sostenible',
    status: 'validation',
    progress: 80,
    teamMembers: 3,
    mentor: {
      name: 'Isabella Chen',
      avatar: 'https://images.pexels.com/photos/1222271/pexels-photo-1222271.jpeg?auto=compress&cs=tinysrgb&w=100&h=100&fit=crop',
      specialty: 'Retail & Sustainability'
    },
    daysUntilPitch: 30
  }
];

export const mockSessions: Session[] = [
  {
    id: '1',
    type: 'mentoring',
    title: 'Weekly Review - EcoTech',
    time: '14:00',
    participants: ['Ana García']
  },
  {
    id: '2',
    type: 'team-meeting',
    title: 'Sprint Planning',
    time: '16:30',
    participants: ['Team AI Platform']
  },
  {
    id: '3',
    type: 'pitch-practice',
    title: 'Pitch Rehearsal',
    time: '18:00',
    participants: ['Sofia Rodriguez']
  }
];

export const mockNotifications: Notification[] = [
  {
    id: '1',
    type: 'info',
    message: 'Nueva solicitud de mentoría recibida',
    time: '2 min',
    read: false
  },
  {
    id: '2',
    type: 'success',
    message: 'Sesión completada con Ana García',
    time: '1 hora',
    read: false
  },
  {
    id: '3',
    type: 'warning',
    message: 'Recordatorio: Reunión en 30 minutos',
    time: '30 min',
    read: true
  }
];