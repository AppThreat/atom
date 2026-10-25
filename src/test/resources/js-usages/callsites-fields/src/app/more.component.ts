import { Component } from "@angular/core";
import { faStar, faCoffee } from "@fortawesome/free-solid-svg-icons";
import { format } from "date-fns";

@Component({
  selector: "app-esc",
  template: '<p class=\'big\'>{{ star }}</p><i [title]="\'star\'"></i>{{ fmt(1) }}',
})
export class EscComponent {
  star = faStar;
  fmt = format;
}

@Component({
  selector: "app-plain",
  templateUrl: "plain.component.html",
})
export class PlainComponent {
  coffee = faCoffee;
  star = faStar;
}

@Component({
  selector: "app-write",
  template: '<b (click)="icon = null"></b><i [icon]="icon"></i>',
})
export class WriteComponent {
  icon = faStar;
}
