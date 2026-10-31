import { Controller, Get, Param, Injectable } from "@nestjs/common";
import { Column, Entity } from "typeorm";
import { EventEmitter } from "events";
import { OnInit } from "@angular/core";

@Entity()
export class Cat {
  @Column()
  name: string;
}

@Controller("cats")
export class CatsController {
  @Get(":id")
  findOne(@Param("id") id: string) {
    return "x";
  }
}

@Injectable()
export class Bus
  extends EventEmitter
  implements OnInit {
}
