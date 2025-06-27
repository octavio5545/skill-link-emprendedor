export interface Usuario {
  id: number;
  nombre: string;
  email: string;
}

export interface Mensaje {
  id: number;
  contenido: string;
  leido: boolean;
  timestampEnvio: string;
  emisor: Usuario;
}

export interface MensajeDTO {
  id: number;
  contenido: string;
  leido: boolean;
  timestampEnvio: string;
  emisorId: number;
  emisorNombre: string;
  emisorEmail: string;
}

export interface Conversacion {
  id: number;
  usuario1: Usuario;
  usuario2: Usuario;
  mensajes?: Mensaje[];
}

export interface ConversacionResumenDTO {
  idConversacion: number;
  idOtroUsuario: number;
  nombreOtroUsuario: string;
  emailOtroUsuario: string;
  ultimoMensaje: string;
  timestampUltimoMensaje: string;
  mensajesNoLeidos: number;
}

export interface CrearConversacionRequest {
  idUsuario1: number;
  idUsuario2: number;
}

export interface EnviarMensajeRequest {
  idConversacion: number;
  idEmisor: number;
  contenido: string;
}

export interface MarcarLeidosRequest {
  idConversacion: number;
  idUsuario: number;
}

export interface MensajeWebSocketDTO {
  idConversacion: number;
  idEmisor: number;
  contenido: string;
}

export interface UsuarioEscribiendoDTO {
  idConversacion: number;
  idUsuario: number;
}

export interface ChatUser {
  id: number;
  name: string;
  email: string;
  avatar?: string;
  role?: 'mentor' | 'colaborador';
  status?: 'online' | 'offline' | 'away';
  expertise?: string[];
}

export interface ChatMessage {
  id: number;
  senderId: number;
  content: string;
  timestamp: Date;
  type: 'text' | 'image' | 'file';
  status: 'sent' | 'delivered' | 'read';
  sender?: ChatUser;
}

export interface ChatConversation {
  id: number;
  participants: ChatUser[];
  lastMessage?: ChatMessage;
  unreadCount: number;
  updatedAt: Date;
  title?: string;
  type: 'direct' | 'group' | 'mentorship';
}