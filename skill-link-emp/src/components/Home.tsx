import { mockPosts } from "../data/mockData";
import { PostCard } from "./Home/PostCard";
import { ProfileCard } from "./Home/ProfileCard";
import { Filter, Plus, Search, Tag, TrendingUp } from "lucide-react";
import { SearchBar } from "./Home/SearchBar";
import { useMemo, useState } from "react";
import { IPost } from "../types/IPost";
import { FilterTag } from "./Home/FilterTag";
import { filterPosts, getAllTags } from "../utils/post";


export const Home = ()=>{
  const [posts, setPosts] = useState<IPost[]>(mockPosts);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [showFilters, setShowFilters] = useState(false);

  const allTags = useMemo(() => getAllTags(posts), [posts]);
  
  const filteredPosts = useMemo(() => 
    filterPosts(posts, searchTerm, selectedTags),
    [posts, searchTerm, selectedTags]
  );

  const handleLike = (postId: string) => {
    setPosts(prevPosts =>
      prevPosts.map(post =>
        post.id === postId
          ? {
              ...post,
              isLiked: !post.likes,
              likes: post.likes ? post.likes - 1 : post.likes + 1
            }
          : post
      )
    );
  };

  

  // const handleComment = (postId: string, content: string) => {
  //   const newComment = {
  //     id: generateUniqueId(),
  //     user: {
  //       id: 'current-user',
  //       name: 'Usuario Actual',
  //       avatar: 'https://images.pexels.com/photos/1542085/pexels-photo-1542085.jpeg?auto=compress&cs=tinysrgb&w=150&h=150&dpr=2',
  //       title: 'Profesional',
  //       company: 'Tu Empresa'
  //     },
  //     content,
  //     timestamp: new Date(),
  //     likes: 0
  //   };

  //   setPosts(prevPosts =>
  //     prevPosts.map(post =>
  //       post.id === postId
  //         ? {
  //             ...post,
  //             comments: [...post.comments, newComment]
  //           }
  //         : post
  //     )
  //   );
  // };

  const handleTagToggle = (tag: string) => {
    setSelectedTags(prev =>
      prev.includes(tag)
        ? prev.filter(t => t !== tag)
        : [...prev, tag]
    );
  };

  const handleClearFilters = () => {
    setSelectedTags([]);
  };
  return(
    <main className="min-h-screen bg-gradient-to-br from-emerald-500 via-teal-600 to-purple-700  px-20 py-8">
      <div className="flex flex-col gap-4 items-center">
        <div className="w-4/5 px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex-1 max-w-2xl">
              <SearchBar
                searchTerm={searchTerm}
                onSearchChange={setSearchTerm}
              />
            </div>

            <div className="flex items-center space-x-4 ml-8">
              <button
                onClick={() => setShowFilters(!showFilters)}
                className={`p-2 rounded-lg transition-colors duration-200 ${
                  showFilters || selectedTags.length > 0
                    ? 'bg-blue-100 text-blue-600'
                    : 'text-gray-600 hover:bg-purple-500'
                }`}
              >
                <Filter className="h-5 w-5 text-white" />
              </button>
              
              <button className="flex items-center space-x-2 bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition-colors duration-200">
                <Plus className="h-4 w-4" />
                <span className="hidden sm:inline">Publicar</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Sidebar */}
          <aside className="lg:col-span-1">
            <div className=" space-y-6">
              {/* Stats Card */}
              <ProfileCard />

              {/* Filters */}
              {showFilters && (
                <FilterTag
                  allTags={allTags}
                  selectedTags={selectedTags}
                  onTagToggle={handleTagToggle}
                  onClearFilters={handleClearFilters}
                />
              )}

              {/* Trending Tags */}
              <div className="bg-white/10 backdrop-blur-md rounded-xl border-white/20 p-6">
                <h3 className="font-semibold text-white mb-4">Etiquetas populares</h3>
                <div className="space-y-2">
                  {allTags.slice(0, 6).map((tag) => (
                    <button
                      key={tag}
                      onClick={() => handleTagToggle(tag)}
                      className={`block w-full text-left px-3 py-2 rounded-lg text-sm transition-colors duration-200 ${
                        selectedTags.includes(tag)
                          ? 'bg-blue-100 text-blue-800'
                          : 'text-white hover:bg-purple-500'
                      }`}
                    >
                      #{tag}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </aside>

          {/* Posts Feed */}
          <div className="lg:col-span-3">
            {/* Search Results Info */}
            {(searchTerm || selectedTags.length > 0) && (
              <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-xl">
                <p className="text-sm text-blue-800">
                  {filteredPosts.length === 0 ? (
                    'No se encontraron publicaciones que coincidan con tu búsqueda.'
                  ) : (
                    `Mostrando ${filteredPosts.length} de ${posts.length} publicaciones`
                  )}
                  {searchTerm && (
                    <span> para "{searchTerm}"</span>
                  )}
                  {selectedTags.length > 0 && (
                    <span> con etiquetas: {selectedTags.join(', ')}</span>
                  )}
                </p>
              </div>
            )}

            {/* Posts */}
            <div className="space-y-6">
              {filteredPosts.length === 0 ? (
                <div className="text-center py-12">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <TrendingUp className="h-8 w-8 text-gray-400" />
                  </div>
                  <h3 className="text-lg font-medium text-gray-900 mb-2">No hay publicaciones</h3>
                  <p className="text-gray-600 max-w-sm mx-auto">
                    No se encontraron publicaciones que coincidan con tus filtros de búsqueda.
                  </p>
                </div>
              ) : (
                filteredPosts.map((post) => (
                  <PostCard key={post.id} post={post} />
                ))
              )}
            </div>
          </div>
        </div>
      </main>
      
    </main>
  )
}