import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../core/components/navbar/navbar.component';
import { FooterComponent } from '../../core/components/footer/footer.component';
import { SidebarFilter } from '../../core/components/sidebar/sidebar.component';
import { ChatbotComponent } from '../../core/components/chatbot/chatbot.component';


@Component({
  selector: 'app-full-layout',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, ChatbotComponent],
  templateUrl: './full-layout.component.html',
  styleUrls: ['./full-layout.component.css']
})
export class FullLayoutComponent {
  isSidebarOpen = false;

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  onFilterChange(filter: SidebarFilter): void {
    console.log('Filter changed:', filter);
    // Phát sự kiện hoặc xử lý filter tại đây
  }
}