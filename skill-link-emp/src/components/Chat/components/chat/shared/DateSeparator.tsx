import React from 'react';

interface DateSeparatorProps {
  date: Date;
}

export const DateSeparator: React.FC<DateSeparatorProps> = ({ date }) => {
  const formatDate = (date: Date): string => {
    if (!date || isNaN(date.getTime())) {
      return 'Fecha inválida';
    }

    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
    
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    
    if (messageDate.getTime() === today.getTime()) {
      return 'Hoy';
    } else if (messageDate.getTime() === yesterday.getTime()) {
      return 'Ayer';
    } else {
      const diffTime = today.getTime() - messageDate.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays <= 7) {
        return date.toLocaleDateString('es-ES', { 
          weekday: 'long'
        });
      } else if (diffDays <= 365) {
        return date.toLocaleDateString('es-ES', { 
          day: 'numeric', 
          month: 'long'
        });
      } else {
        return date.toLocaleDateString('es-ES', { 
          day: 'numeric', 
          month: 'long', 
          year: 'numeric'
        });
      }
    }
  };

  return (
    <div className="flex items-center justify-center my-6">
      <div className="bg-white/80 backdrop-blur-sm border border-gray-200 rounded-full px-4 py-2 shadow-sm">
        <span className="text-sm font-medium text-gray-600">
          {formatDate(date)}
        </span>
      </div>
    </div>
  );
};