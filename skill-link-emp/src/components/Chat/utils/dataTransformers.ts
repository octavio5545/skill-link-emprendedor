import type { 
  ConversacionResumenDTO, 
  Mensaje, 
  MensajeDTO,
  Usuario,
  ChatUser, 
  ChatMessage, 
  ChatConversation 
} from '../types/api';
import { getAvatarForUser, mapBackendRoleToFrontend } from '../constants/avatars';

export const transformUsuarioToChatUser = (usuario: Usuario): ChatUser => {
  return {
    id: usuario.id,
    name: usuario.nombre,
    email: usuario.email,
    avatar: getAvatarForUser(usuario.id),
    role: 'colaborador',
    status: 'online',
    expertise: []
  };
};

export const transformUsuarioCompletoToChatUser = (
  id: number, 
  nombre: string, 
  email: string, 
  role?: string
): ChatUser => {
  return {
    id,
    name: nombre,
    email,
    avatar: getAvatarForUser(id),
    role: role ? mapBackendRoleToFrontend(role) : 'colaborador',
    status: 'online',
    expertise: []
  };
};

const parseBackendTimestamp = (timestamp: string): Date => {
  const utcTimestamp = timestamp.includes('Z') ? timestamp : timestamp + 'Z';
  return new Date(utcTimestamp);
};

export const transformMensajeDTOToChatMessage = (mensajeDTO: MensajeDTO): ChatMessage => {
  if (!mensajeDTO || typeof mensajeDTO.id === 'undefined') {
    throw new Error('MensajeDTO sin ID válido');
  }

  const emisor: Usuario = {
    id: mensajeDTO.emisorId,
    nombre: mensajeDTO.emisorNombre,
    email: mensajeDTO.emisorEmail
  };

  return {
    id: mensajeDTO.id,
    senderId: mensajeDTO.emisorId,
    content: mensajeDTO.contenido || '',
    timestamp: parseBackendTimestamp(mensajeDTO.timestampEnvio.toString()),
    type: 'text',
    status: mensajeDTO.leido ? 'read' : 'delivered',
    sender: transformUsuarioToChatUser(emisor)
  };
};

export const transformMensajeToChatMessage = (mensaje: any): ChatMessage => {
  if (!mensaje || typeof mensaje.id === 'undefined') {
    console.error('Mensaje inválido:', mensaje);
    throw new Error('Mensaje sin ID válido');
  }

  let emisorData: Usuario;
  
  if (mensaje.emisor && mensaje.emisor.id) {
    console.log('Mensaje con emisor completo detectado');
    emisorData = {
      id: mensaje.emisor.id,
      nombre: mensaje.emisor.nombre || mensaje.emisor.name,
      email: mensaje.emisor.email
    };
  } else {
    console.error('MENSAJE SIN EMISOR VÁLIDO:', mensaje);
    console.error('Emisor recibido:', mensaje.emisor);
    
    emisorData = {
      id: 0,
      nombre: 'Usuario Desconocido',
      email: 'unknown@example.com'
    };
    
    console.warn('Usando emisor por defecto temporal');
  }

  const chatMessage = {
    id: mensaje.id,
    senderId: emisorData.id,
    content: mensaje.contenido || '',
    timestamp: parseBackendTimestamp(mensaje.timestampEnvio),
    type: 'text' as const,
    status: mensaje.leido ? 'read' as const : 'delivered' as const,
    sender: transformUsuarioToChatUser(emisorData)
  };
  return chatMessage;
};

export const transformConversacionResumenToChatConversation = (
  resumen: ConversacionResumenDTO,
  currentUserId: number
): ChatConversation => {
  
  const currentUser: ChatUser = {
    id: currentUserId,
    name: 'Tú',
    email: '',
    avatar: getAvatarForUser(currentUserId),
    role: 'colaborador',
    status: 'online'
  };

  const otherUser: ChatUser = {
    id: resumen.idOtroUsuario,
    name: resumen.nombreOtroUsuario,
    email: resumen.emailOtroUsuario, 
    avatar: getAvatarForUser(resumen.idOtroUsuario),
    role: 'colaborador',
    status: 'online'
  };

  const lastMessageSenderId = resumen.idOtroUsuario;
  
  const lastMessage: ChatMessage = {
    id: 0,
    senderId: lastMessageSenderId,
    content: resumen.ultimoMensaje,
    timestamp: parseBackendTimestamp(resumen.timestampUltimoMensaje.toString()),
    type: 'text',
    status: 'read',
    sender: lastMessageSenderId === currentUserId ? currentUser : otherUser
  };

  return {
    id: resumen.idConversacion,
    participants: [currentUser, otherUser],
    lastMessage,
    unreadCount: resumen.mensajesNoLeidos,
    updatedAt: parseBackendTimestamp(resumen.timestampUltimoMensaje.toString()),
    title: resumen.nombreOtroUsuario,
    type: 'direct'
  };
};

export const transformMensajesDTOToChatMessages = (mensajesDTO: MensajeDTO[]): ChatMessage[] => {
  if (!Array.isArray(mensajesDTO)) {
    console.error('Los mensajes no son un array:', mensajesDTO);
    return [];
  }

  return mensajesDTO
    .filter(mensajeDTO => mensajeDTO && typeof mensajeDTO.id !== 'undefined')
    .map(mensajeDTO => {
      try {
        return transformMensajeDTOToChatMessage(mensajeDTO);
      } catch (error) {
        console.error('Error transformando mensajeDTO:', mensajeDTO, error);
        return null;
      }
    })
    .filter((mensaje): mensaje is ChatMessage => mensaje !== null);
};

export const transformMensajesToChatMessages = (mensajes: Mensaje[]): ChatMessage[] => {
  if (!Array.isArray(mensajes)) {
    console.error('Los mensajes no son un array:', mensajes);
    return [];
  }

  return mensajes
    .filter(mensaje => mensaje && typeof mensaje.id !== 'undefined')
    .map(mensaje => {
      try {
        return transformMensajeToChatMessage(mensaje);
      } catch (error) {
        console.error('Error transformando mensaje:', mensaje, error);
        return null;
      }
    })
    .filter((mensaje): mensaje is ChatMessage => mensaje !== null);
};