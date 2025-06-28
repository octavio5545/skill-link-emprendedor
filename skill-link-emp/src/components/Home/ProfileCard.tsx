import { getUserAvatar } from '../Post/utils/avatarUtils';

interface ProfileCardProps {
  name: string | null;
  rol: string | null;
  currentUserId?: string | null;
}


export const ProfileCard = ({name, rol, currentUserId}: ProfileCardProps )=>{
  const userAvatar = getUserAvatar(currentUserId ?? '');

  const getCapitalizedName = (inputName: string | null) => {
    if (!inputName) {
      return 'Invitado';
    }
    return inputName.charAt(0).toUpperCase() + inputName.slice(1);
  };

  const capitalizedName = getCapitalizedName(name);

  return(
    <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20">
      <div className="flex flex-row gap-2 pb-4">
        <img
          className="w-12 h-12 rounded-full object-cover" 
          src={userAvatar} alt="Profile" />
        <div className="flex flex-col gap-2">
          <h3 className="text-white font-semibold">{capitalizedName}</h3>
          <p className="text-white/70 text-sm">{rol}</p>
        </div>
      </div>
    <h3 className="font-semibold text-white mb-4">Estadísticas</h3>
    <div className="space-y-3">
      <div className="flex justify-between">
        <span className="text-white/70 text-sm">Publicaciones</span>
        <span className="font-medium text-white">{7}</span>
      </div>
      <div className="flex justify-between">
        <span className="text-white/70 text-sm">Mentorías</span>
        <span className="font-medium text-white">{6}</span>
      </div>
      <div className="flex justify-between">
        <span className="text-white/70 text-sm">Proyectos</span>
        <span className="font-medium text-white">{4}</span>
      </div>
    </div>
  </div>
  )
}

