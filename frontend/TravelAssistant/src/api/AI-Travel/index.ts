
import http from "../../utils/http/index"

export function clearMemory(conversationId: string) {
 return http.delete(`/travel/memory?conversationId=${conversationId}`);
}