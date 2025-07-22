import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WebSocketService]
    });
    service = TestBed.inject(WebSocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have connect method', () => {
    expect(service.connect).toBeDefined();
  });

  it('should have disconnect method', () => {
    expect(service.disconnect).toBeDefined();
  });

  it('should have subscribe method', () => {
    expect(service.subscribe).toBeDefined();
  });

  it('should have publish method', () => {
    expect(service.publish).toBeDefined();
  });

  it('should have isConnected method', () => {
    expect(service.isConnected).toBeDefined();
  });

  it('should have waitForConnection method', () => {
    expect(service.waitForConnection).toBeDefined();
  });
}); 