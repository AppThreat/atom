import { Component } from "@angular/core";
import { faCoffee, faStar } from "@fortawesome/free-solid-svg-icons";
import { formatDistance } from "date-fns";

@Component({
  selector: "app-icons",
  templateUrl: "./icons.component.html",
})
export class IconsComponent {
  readonly faCoffee = faCoffee;
  readonly when = formatDistance;
}

@Component({
  selector: "app-star",
  template: `<fa-icon [icon]="faStar"></fa-icon> {{ faStar.iconName | uppercase }}`,
})
export class StarComponent {
  faStar = faStar;
}
