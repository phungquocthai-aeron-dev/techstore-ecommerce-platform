// ─── Conversation ─────────────────────────────────────────────────────────────

export interface ParticipantInfo {
  userId: string;
  username: string;
  avatar: string;
  userType: string; // 'INTERNAL' | 'EXTERNAL'
}

export interface ConversationResponse {
  id: string;
  type: string; // 'DIRECT' | 'GROUP'
  participantsHash: string;
  conversationAvatar: string;
  conversationName: string;
  participants: ParticipantInfo[];
  createdDate: string;
  modifiedDate: string;
}

export interface ConversationRequest {
  type: string;
  userCurrentType: string;
  userTargetType: string;
  participantIds: string[];
}

// ─── Message ───────────────────────────────────────────────────────────────────

export interface ChatMessageResponse {
  id: string;
  conversationId: string;
  me: boolean;
  message: string;
  sender: ParticipantInfo;
  createdDate: string;
}

export interface ChatMessageRequest {
  conversationId: string;
  userType: string;
  message: string;
}