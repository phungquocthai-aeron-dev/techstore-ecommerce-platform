import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../core/components/navbar/navbar.component';
import { FooterComponent } from '../../core/components/footer/footer.component';
import { SidebarFilter } from '../../core/components/sidebar/sidebar.component';
import { ChatbotComponent } from '../../core/components/chatbot/chatbot.component';
import { ChatComponent } from '../../features/chat/chat.component';

import { TokenService } from '../../core/services/token.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-full-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    NavbarComponent,
    FooterComponent,
    ChatbotComponent,
    ChatComponent,
    NgIf
  ],
  templateUrl: './full-layout.component.html',
  styleUrls: ['./full-layout.component.css']
})
export class FullLayoutComponent implements OnInit {
  isSidebarOpen = false;
  isLoggedIn = false;

  constructor(private tokenService: TokenService) {}

  ngOnInit(): void {
    this.tokenService.getLoggedIn().subscribe(status => {
      this.isLoggedIn = status;
    });
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  onFilterChange(filter: SidebarFilter): void {
    console.log('Filter changed:', filter);
  }
}