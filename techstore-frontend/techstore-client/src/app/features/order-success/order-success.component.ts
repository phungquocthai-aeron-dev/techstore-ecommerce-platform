import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

export interface ConfettiItem {
  style: string;
}

@Component({
  selector: 'app-order-success',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-success.component.html',
  styleUrl: './order-success.component.css'
})
export class OrderSuccessComponent implements OnInit {

  txnRef = '';
  copied = false;
  confettiItems: ConfettiItem[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.txnRef = this.route.snapshot.queryParamMap.get('txnRef') ?? '—';
    this.generateConfetti();
  }

  copyTxnRef(): void {
    navigator.clipboard.writeText(this.txnRef).then(() => {
      this.copied = true;
      setTimeout(() => (this.copied = false), 2000);
    });
  }

  generateConfetti(): void {
    const colors = ['#38bdf8', '#4ade80', '#fbbf24', '#f472b6', '#a78bfa'];
    this.confettiItems = Array.from({ length: 40 }, () => {
      const color = colors[Math.floor(Math.random() * colors.length)];
      const left  = Math.random() * 100;
      const size  = 4 + Math.random() * 6;
      const delay = Math.random() * 2;
      const dur   = 2 + Math.random() * 2;
      return {
        style: `left:${left}%;top:-10px;width:${size}px;height:${size}px;background:${color};animation-duration:${dur}s;animation-delay:${delay}s;`
      };
    });
  }

  goOrders(): void { this.router.navigate(['/orders']); }
  goHome():   void { this.router.navigate(['/']); }
}