import { Heart, MessageCircle, Share } from 'lucide-react';
import { IPost } from '../types/IPost';


interface PostCardProps{
  post: IPost
}

export const PostCard = ({post}: PostCardProps)=>{
  return(
    <div className="flex flex-col bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 hover:bg-white/15 transition-all duration-300">
     <div className='flex flex-row gap-2'>
        <img src={post.authorImage} alt={post.title} className="w-10 h-10 rounded-full" />
        <div>
          <p>{post.author}</p>
          <p>{post.authorRole}</p>
          <p>{post.date}</p>
        </div>
     </div>
     <div className='flex flex-col gap-2'>
      <h3>{post.title}</h3>
      <p>{post.description}</p>
      <img src={post.image} alt={post.author} className="w-full h-48 object-cover rounded-md" />
     </div>
     <div className='flex flex-row gap-2'>
      <div>
        <Heart className="w-5 h-5 text-white/70" onClick={()=>{
          console.log("Liked");
        }} />
        <p>{post.likes}</p>
      </div>
      <div>
        <MessageCircle className="w-5 h-5 text-white/70" onClick={()=>{
          console.log("Commented");
        }} />
        <p>{post.comments}</p>
      </div>
     </div>
    </div>
  )
}